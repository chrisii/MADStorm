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
package com.bt.mindstorm;

/**
 * Interface for the lego pin where the sensors are plugged in.
 * 
 * @author Juerg Luthiger
 * 
 */
public interface SensorPin {
	/**
	 * Returns the actual pin number of the actor.
	 * 
	 * @return pin number
	 */
	byte getPinNr();
}
