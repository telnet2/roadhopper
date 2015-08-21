/*
 * Copyright (c) 2015 Andreas Wolf
 *
 * See te LICENSE file in the project root for further copyright information.
 */

package info.andreaswolf.roadhopper.simulation.control

import akka.actor.{ActorLogging, ActorRef}
import akka.pattern.ask
import info.andreaswolf.roadhopper.simulation.signals.SignalBus.UpdateSignalValue
import info.andreaswolf.roadhopper.simulation.signals.{Process, SignalState}

import scala.concurrent.Future

/**
 * An integrator controller summarizes its input value over time.
 *
 * TODO implement a proportionality factor
 */
class Integrator(inputSignalName: String, outputSignalName: String, signalBus: ActorRef) extends Process(Some(signalBus))
with ActorLogging {

	import context.dispatcher

	val initialState = new ControllerState[Double](0.0, 0)

	var currentState: Option[ControllerState[Double]] = None

	/**
	 * This value becomes the current state with the first invocation of [[timeAdvanced()]].
	 */
	var nextState: Option[ControllerState[Double]] = Some(initialState)

	override def timeAdvanced(oldTime: Int, newTime: Int): Future[Unit] = Future {
		currentState = nextState
		nextState = null
	}

	/**
	 * The central routine of a process. This is invoked whenever a subscribed signal’s value changes.
	 */
	override def invoke(signals: SignalState): Future[Any] = {
		val deltaT = time - currentState.get.time
		if (deltaT == 0) {
			return Future.successful()
		}
		val currentInput = signals.signalValue(inputSignalName, 0.0)

		nextState = deriveNewState(deltaT, currentInput)

		signalBus ? UpdateSignalValue(outputSignalName, nextState.get.value)
	}

	def deriveNewState(deltaT: Int, currentInput: Double): Option[ControllerState[Double]] = {
		Some(new ControllerState[Double](currentState.get.value + currentInput * deltaT / 1000.0, time))
	}
}
