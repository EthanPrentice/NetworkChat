package com.ethanprentice.networkchat.message_router

import com.ethanprentice.networkchat.adt.Endpoint
import com.ethanprentice.networkchat.adt.Message
import com.ethanprentice.networkchat.message_router.mocks.BarHandler
import com.ethanprentice.networkchat.message_router.mocks.FooHandler
import com.ethanprentice.networkchat.utils.Mocking
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class EndpointManagerUnitTest {

    @Before
    fun initialize() {
        Mocking.mockLog()
    }

    @Test
    fun testHandler() {
        val eManager = EndpointManager()

        val fooHandler = FooHandler(eManager)
        val barHandler = BarHandler(eManager)

        Assert.assertFalse(eManager.containsHandler(fooHandler))
        Assert.assertFalse(eManager.containsHandler(barHandler))

        eManager.registerHandler(fooHandler)
        Assert.assertTrue(eManager.containsHandler(fooHandler))
        Assert.assertFalse(eManager.containsHandler(barHandler))

        // test reregistering has no negative effects
        eManager.registerHandler(fooHandler)
        Assert.assertTrue(eManager.containsHandler(fooHandler))
        Assert.assertFalse(eManager.containsHandler(barHandler))

        eManager.registerHandler(barHandler)
        Assert.assertTrue(eManager.containsHandler(fooHandler))
        Assert.assertTrue(eManager.containsHandler(barHandler))
    }


    @Test
    fun testEndpoints() {
        val eManager = EndpointManager()

        val fooHandler = FooHandler(eManager)
        val barHandler = BarHandler(eManager)

        eManager.registerHandler(fooHandler)
        eManager.registerHandler(barHandler)

        val fooEndpoint1 = Endpoint("com.ethanprentice.networkchat/foo/endpoint1", Message::class)
        val fooEndpoint2 = Endpoint("com.ethanprentice.networkchat/foo/endpoint2", Message::class)

        val barEndpoint1 = Endpoint("com.ethanprentice.networkchat/bar/endpoint1", Message::class)

        Assert.assertFalse(eManager.containsEndpoint(fooEndpoint1))
        Assert.assertFalse(eManager.containsEndpoint(fooEndpoint2))
        Assert.assertFalse(eManager.containsEndpoint(barEndpoint1))

        eManager.registerEndpoint(fooEndpoint1)
        Assert.assertTrue(eManager.containsEndpoint(fooEndpoint1))
        Assert.assertFalse(eManager.containsEndpoint(fooEndpoint2))
        Assert.assertFalse(eManager.containsEndpoint(barEndpoint1))

        eManager.registerEndpoint(fooEndpoint2)
        Assert.assertTrue(eManager.containsEndpoint(fooEndpoint1))
        Assert.assertTrue(eManager.containsEndpoint(fooEndpoint2))
        Assert.assertFalse(eManager.containsEndpoint(barEndpoint1))

        eManager.registerEndpoint(barEndpoint1)
        Assert.assertTrue(eManager.containsEndpoint(fooEndpoint1))
        Assert.assertTrue(eManager.containsEndpoint(fooEndpoint2))
        Assert.assertTrue(eManager.containsEndpoint(barEndpoint1))

    }
}