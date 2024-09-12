package com.ivanb.aktors

sealed interface Behaviour<in T> {
    fun <A : T> ifSameThen(other: Behaviour<A>): Behaviour<A> = if (this == Behaviours.Same) other else this
}

object Behaviours {
    /**
     * Receive a message and return a Behaviour describing how to handle the next message
     * */
    fun <T> receiveMessage(handler: suspend (T) -> Behaviour<T>): Behaviour<T> = ReceiveMessage(handler)

    fun <T> setup(init: () -> Behaviour<T>): Behaviour<T> = Setup(init)

    @Suppress("UNCHECKED_CAST")
    fun <T> same(): Behaviour<T> = Same as Behaviour<T>

    @Suppress("UNCHECKED_CAST")
    fun <T> stopped(): Behaviour<T> = Stopped as Behaviour<T>

    class ReceiveMessage<T>(
        val handler: suspend (T) -> Behaviour<T>,
    ) : Behaviour<T>

    /**
     * Behavoir is unchanged
     * */
    data object Same : Behaviour<Nothing>

    data object Stopped : Behaviour<Nothing>

    class Setup<T>(
        val init: () -> Behaviour<T>,
    ) : Behaviour<T>
}
