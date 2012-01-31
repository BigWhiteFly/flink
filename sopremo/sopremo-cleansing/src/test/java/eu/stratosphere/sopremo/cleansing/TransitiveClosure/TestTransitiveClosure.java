/***********************************************************************************************************************
 *
 * Copyright (C) 2010 by the Stratosphere project (http://stratosphere.eu)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 **********************************************************************************************************************/
package eu.stratosphere.sopremo.cleansing.TransitiveClosure;

import eu.stratosphere.sopremo.CompositeOperator;
import eu.stratosphere.sopremo.InputCardinality;
import eu.stratosphere.sopremo.JsonStream;
import eu.stratosphere.sopremo.SopremoModule;
import eu.stratosphere.sopremo.base.UnionAll;
import eu.stratosphere.sopremo.cleansing.transitiveClosure.EmitMatrix;
import eu.stratosphere.sopremo.cleansing.transitiveClosure.GenerateMatrix;
import eu.stratosphere.sopremo.cleansing.transitiveClosure.Phase1;
import eu.stratosphere.sopremo.cleansing.transitiveClosure.Phase2;
import eu.stratosphere.sopremo.cleansing.transitiveClosure.Phase3;

@InputCardinality(min = 2, max = 2)
public class TestTransitiveClosure extends CompositeOperator<TestTransitiveClosure> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6206345352285312621L;

	private int numberOfPartitions = 1;

	public void setNumberOfPartitions(int number) {
		this.numberOfPartitions = number;
	}

	/*
	 * (non-Javadoc)
	 * @see eu.stratosphere.sopremo.CompositeOperator#asElementaryOperators()
	 */
	@Override
	public SopremoModule asElementaryOperators() {
		final SopremoModule sopremoModule = new SopremoModule(this.getName(), 2, 1);
		JsonStream input = sopremoModule.getInput(0);
		JsonStream nullInput = sopremoModule.getInput(1);

		// Preprocessing
		final GenerateMatrix filledMatrix = new GenerateMatrix().withInputs(input, nullInput);
		filledMatrix.setNumberOfPartitions(this.numberOfPartitions);

		Phase1[] phase1 = new Phase1[this.numberOfPartitions];
		Phase2[] phase2 = new Phase2[this.numberOfPartitions];
		Phase3[] phase3 = new Phase3[this.numberOfPartitions];
		UnionAll itOutput[] = new UnionAll[this.numberOfPartitions];

		for (int i = 0; i < this.numberOfPartitions; i++) {
			JsonStream inputStream = i == 0 ? filledMatrix : phase3[i - 1];

			// compute transitive Closure P1
			phase1[i] = new Phase1().withInputs(inputStream);
			phase1[i].setIterationStep(i);

			// compute transitive Closure P2
			phase2[i] = new Phase2().withInputs(phase1[i]);
			phase2[i].setIterationStep(i);

			// compute transitive Closure P3
			phase3[i] = new Phase3().withInputs(phase2[i]);
			phase3[i].setNumberOfPartitions(i);

		}

		// emit Results as Links
		final EmitMatrix result;
		if (this.phase == 1) {
			result = new EmitMatrix().withInputs(phase1);
		} else {
			if (this.phase == 2) {
				result = new EmitMatrix().withInputs(phase2);
			} else {
				result = new EmitMatrix().withInputs(phase3);
			}
		}

		sopremoModule.getOutput(0).setInput(0, result);

		return sopremoModule;
	}

	private int phase;

	public void setPhase(int n) {
		this.phase = n;
	}

}
