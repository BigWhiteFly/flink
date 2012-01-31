package eu.stratosphere.sopremo.type;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import eu.stratosphere.sopremo.pact.SopremoUtil;

public class ArrayNode extends JsonNode implements Iterable<JsonNode> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 898220542834090837L;

	private List<JsonNode> children = new ArrayList<JsonNode>();

	public ArrayNode() {
	}

	public ArrayNode(final JsonNode... nodes) {
		for (final JsonNode node : nodes)
			this.add(node);
	}

	public ArrayNode(final Collection<? extends JsonNode> nodes) {
		for (final JsonNode node : nodes)
			this.add(node);
	}

	public int size() {
		return this.children.size();
	}

	public ArrayNode add(final JsonNode node) {
		if (node == null)
			throw new NullPointerException();
		this.children.add(node);
		return this;
	}

	public void add(final int index, final JsonNode element) {
		this.children.add(index, element);
	}

	public JsonNode get(final int index) {
		if (0 <= index && index < this.children.size())
			return this.children.get(index);
		throw new ArrayIndexOutOfBoundsException();
	}

	public JsonNode set(final int index, final JsonNode node) {
		if (node == null)
			throw new NullPointerException();
		return this.children.set(index, node);
	}

	public JsonNode remove(final int index) {
		if (0 <= index && index < this.children.size())
			return this.children.remove(index);
		throw new ArrayIndexOutOfBoundsException();
	}

	public void clear() {
		this.children.clear();
	}

	@Override
	public StringBuilder toString(final StringBuilder sb) {
		sb.append('[');

		for (int i = 0; i < this.children.size(); i++) {
			if (i > 0)
				sb.append(',');
			this.children.get(i).toString(sb);
		}

		sb.append(']');
		return sb;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + this.children.hashCode();
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (this.getClass() != obj.getClass())
			return false;

		final ArrayNode other = (ArrayNode) obj;
		if (!this.children.equals(other.children))
			return false;
		return true;
	}

	@Override
	public void read(final DataInput in) throws IOException {
		this.children.clear();
		final int len = in.readInt();

		for (int i = 0; i < len; i++) {
			JsonNode node;
			try {
				node = Type.values()[in.readInt()].getClazz().newInstance();
				node.read(in);
				this.add(node.canonicalize());
			} catch (final InstantiationException e) {
				e.printStackTrace();
			} catch (final IllegalAccessException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public List<JsonNode> getJavaValue() {
		return this.children;
	}

	@Override
	public void write(final DataOutput out) throws IOException {
		out.writeInt(this.children.size());

		for (final JsonNode child : this.children) {
			SopremoUtil.serializeNode(out, child);
		}
	}

	@Override
	public ArrayNode clone() {
		final ArrayNode clone = (ArrayNode) super.clone();
		clone.children = new ArrayList<JsonNode>(this.children);
		final ListIterator<JsonNode> listIterator = clone.children.listIterator();
		while (listIterator.hasNext())
			listIterator.set(listIterator.next().clone());
		return clone;
	}

	@Override
	public Iterator<JsonNode> iterator() {
		return this.children.iterator();
	}

	@Override
	public boolean isArray() {
		return true;
	}

	public boolean isEmpty() {
		return this.children.isEmpty();
	}

	public static ArrayNode valueOf(final Iterator<JsonNode> iterator) {
		final ArrayNode array = new ArrayNode();
		while (iterator.hasNext())
			array.add(iterator.next());
		return array;
	}

	public JsonNode[] toArray() {
		return this.children.toArray(new JsonNode[this.children.size()]);
	}

	public ArrayNode addAll(final Collection<? extends JsonNode> c) {
		for (final JsonNode jsonNode : c)
			this.add(jsonNode);
		return this;
	}

	@Override
	public Type getType() {
		return Type.ArrayNode;
	}

	@Override
	public int compareToSameType(final JsonNode other) {
		// if(!(other instanceof ArrayNode)){
		// return -1;
		// }
		final ArrayNode node = (ArrayNode) other;
		if (node.size() != this.size())
			return this.size() - node.size();
		for (int i = 0; i < this.size(); i++) {
			final int comp = this.get(i).compareTo(node.get(i));
			if (comp != 0)
				return comp;
		}
		return 0;
	}

	public JsonNode subList(final int fromIndex, final int toIndex) {
		return new ArrayNode(this.children.subList(fromIndex, toIndex));
	}

}
