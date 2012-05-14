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
package com.bt.mindstorm.robot;

/**
 * Base interface holding important properties for the task itself.
 *  
 * @author Juerg Luthiger
 *
 */
public interface RobotMotionTask extends Runnable {
	/**
	 * Returns the delay after which the task is going to run.
	 * 
	 * @return the delay
	 */
	public long getDelay();
	/**
	 * Returns the time period of the task.
	 * 
	 * @return the time period
	 */
	public long getPeriod();
	/**
	 * Command to start the task.
	 */
	public void run();
}
