package edu.elon.herps;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

public class Parameters implements Serializable, Iterable<Object> {
	private static final long serialVersionUID = 1L;

	private ArrayList<Object> params = new ArrayList<Object>();

	/**
	 * Gets the number of parameters in this set.
	 * @return The size
	 */
	public int getSize() {
		return params.size();
	}

	public void addParam(Object param) {
		params.add(param);
	}

	public Object getObject() { return getObject(0); }

	public Object getObject(int index) {
		return params.get(index);
	}

	/**
	 * Gets the first parameter, cast as a boolean.
	 * @return The parameter
	 */
	public boolean getBoolean() { return getBoolean(0); }
	/**
	 * Gets the parameter at the given index, cast as a boolean.
	 * @param index The index
	 * @return The parameter
	 */
	public boolean getBoolean(int index) {
		return (Boolean)params.get(index);
	}

	/**
	 * Gets the first parameter, cast as a String.
	 * @return The parameter
	 */
	public String getString() { return getString(0); }
	/**
	 * Gets the parameter at the given index, cast as a String.
	 * @param index The index
	 * @return The parameter
	 */
	public String getString(int index) {
		return (String)params.get(index);
	}

	/**
	 * Gets the first parameter, cast as an int.
	 * @return The parameter
	 */
	public int getInt() { return getInt(0); }
	/**
	 * Gets the parameter at the given index, cast as an int.
	 * @param index The index
	 * @return The parameter
	 */
	public int getInt(int index) {
		return (Integer)params.get(index);
	}

	/**
	 * Gets the first parameter, cast as a float.
	 * @return The parameter
	 */
	public float getFloat() { return getFloat(0); }
	/**
	 * Gets the parameter at the given index, cast as a float.
	 * @param index The index
	 * @return The parameter
	 */
	public float getFloat(int index) {
		return (Float)params.get(index);
	}

	public Double getDouble() {
		return (Double)params.get(0);
	}
	
	public Double getDouble(int index) {
		return (Double)params.get(index);
	}
	
	/**
	 * Gets the first parameter, cast as another set of Parameters.
	 * This is used when a list or complex data is passed as a parameter.
	 * @return The parameter
	 */
	public Parameters getParameters() { return getParameters(0); }
	/**
	 * Gets the parameter at the given index, cast as another set of Parameters.
	 * This is used when a list or complex data is passed as a parameter.
	 * @param index The index
	 * @return The parameter
	 */
	public Parameters getParameters(int index) {
		return (Parameters)params.get(index);
	}

	@Override
	public String toString() {
		return Arrays.toString(params.toArray());
	}
	
	@Override
	public Iterator iterator() {
		return new Iterator();
	}

	public boolean equals(Parameters o) {
		if (o.getSize() != getSize())
			return false;

		try {
			for (int i = 0; i < o.params.size(); i++) {
				if (params.get(i) instanceof Parameters) {
					if (!getParameters(i).equals(o.getParameters(i))) {
						return false;
					}
				} else {
					if (!getObject(i).equals(o.getObject(i))) {
						return false;
					}
				}
			}
		}
		catch (Exception e) {
			return false;
		}

		return true;
	}

	public Parameters copy() {
		Parameters o = new Parameters();
		for (int i = 0; i < params.size(); i++) {
			if (params.get(i) instanceof Parameters) {
				o.addParam(getParameters(i).copy());
			} else {
				o.addParam(params.get(i));
			}
		}
		return o;
	}
	
	public class Iterator implements java.util.Iterator<Object>{
		private int index = 0;
		private Parameters params = copy();

		public int getSize() {
			return params.getSize();
		}

		public Object getObject() { return params.getObject(index++); }
		public boolean getBoolean() { return params.getBoolean(index++); }
		public String getString() { return params.getString(index++); }
		public int getInt() { return params.getInt(index++); }
		public float getFloat() { return params.getFloat(index++); }
		public Double getDouble() {	return params.getDouble(index++); }
		public Parameters getParameters() { return params.getParameters(index++); }

		@Override
		public boolean hasNext() {
			return index < getSize();
		}

		@Override
		public Object next() {
			return getObject();
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
}
