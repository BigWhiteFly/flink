package eu.stratosphere.sopremo.expressions;

import static eu.stratosphere.sopremo.JsonUtil.createObjectNode;
import static eu.stratosphere.sopremo.JsonUtil.createValueNode;
import junit.framework.Assert;

import org.junit.Test;

import eu.stratosphere.sopremo.DefaultFunctions;
import eu.stratosphere.sopremo.type.ArrayNode;
import eu.stratosphere.sopremo.type.JsonNode;

public class GroupingExpressionTest extends EvaluableExpressionTest<GroupingExpression> {
	@Override
	protected GroupingExpression createDefaultInstance(final int index) {
		return new GroupingExpression(new ConstantExpression(index), EvaluationExpression.NULL);
	}

	@Test
	public void testAggregation() {
		this.context.getFunctionRegistry().register(DefaultFunctions.class);

		final ArrayNode input = new ArrayNode();
		input.add(createObjectNode("key", 1, "value", 11));
		input.add(createObjectNode("key", 2, "value", 24));
		input.add(createObjectNode("key", 3, "value", 33));
		input.add(createObjectNode("key", 2, "value", 25));
		input.add(createObjectNode("key", 1, "value", 12));

		final GroupingExpression aggExpression = new GroupingExpression(new ObjectAccess("key"), new MethodCall("sum",
			new ArrayProjection(new ObjectAccess("value"))));

		final JsonNode result = aggExpression.evaluate(input, this.context);

		final ArrayNode expected = new ArrayNode();
		expected.add(createValueNode(23));
		expected.add(createValueNode(49));
		expected.add(createValueNode(33));

		Assert.assertEquals(expected, result);
	}
}
