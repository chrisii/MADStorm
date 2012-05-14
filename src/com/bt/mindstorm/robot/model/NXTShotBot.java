
/*
 * Copyright 2002-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.bt.mindstorm.robot.model;

import java.util.ArrayList;
import java.util.List;

import com.bt.mindstorm.nxt.NXT;
import com.bt.mindstorm.nxt.NXT.NXTActorPin;
import com.bt.mindstorm.robot.motor.Actor;
import com.bt.mindstorm.robot.sensor.Sensor;

/**
 * Robot derived from {@link NXTCastorBot}. It adds an actor on pin a to
 * drive motor to shoot balls.
 * 
 * @author Juerg Luthiger
 *
 */
public class NXTShotBot extends NXTCastorBot {
	private int DIR = 1;
	
	List<Sensor> sensors = new ArrayList<Sensor>();
	private Actor motorA;

	public NXTShotBot(NXT nxt) {
		super(nxt);
	}
	
	@Override
	protected int getDirection() {
		return DIR;
	}

	@Override
	protected void createMotors() {
		super.createMotors();
		motorA = nxt.createActor(NXTActorPin.PIN_A);
	}

	/**
	 * Shoot balls
	 */
	@Override
	public void action(boolean isStartAction) {
		if (isStartAction) {
			motorA.setSpeed(100);
		} else {
			motorA.setSpeed(0);
		}
	}
}