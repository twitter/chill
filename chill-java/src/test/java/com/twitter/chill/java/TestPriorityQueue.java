package com.twitter.chill.java;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.util.Comparator;
import java.util.PriorityQueue;

public class TestPriorityQueue {

	private static final Kryo kryo = new Kryo();

	private static final PriorityQueue<String> test_priority_queue1 =
			new PriorityQueue<>((o1, o2) -> o1.length() - o2.length() - 1);

	private static final PriorityQueue<String> test_priority_queue2 =
			new PriorityQueue<>(new Comparator<String>() {
				@Override
				public int compare(String o1, String o2) {
					return o1.length() - o2.length() - 1;
				}
			});

	static {
		test_priority_queue1.add("12345");
		test_priority_queue1.add("123");
		test_priority_queue1.add("12456789");

		test_priority_queue2.add("12345");
		test_priority_queue2.add("123");
		test_priority_queue2.add("12456789");
	}

	public static PriorityQueue<String> serializeAndDeserializeQueue1() {
		Output output = new Output(1000, -1);
		kryo.writeClassAndObject(output, test_priority_queue1);
		Input input = new Input(output.toBytes());
		return (PriorityQueue<String>) kryo.readClassAndObject(input);
	}

	public static PriorityQueue<String> serializeAndDeserializeQueue1(Kryo kryo) {
		Output output = new Output(1000, -1);
		kryo.writeClassAndObject(output, test_priority_queue1);
		Input input = new Input(output.toBytes());
		return (PriorityQueue<String>) kryo.readClassAndObject(input);
	}

	public static PriorityQueue<String> serializeAndDeserializeQueue2(Kryo kryo) {
		Output output = new Output(1000, -1);
		kryo.writeClassAndObject(output, test_priority_queue2);
		Input input = new Input(output.toBytes());
		return (PriorityQueue<String>) kryo.readClassAndObject(input);
	}
}
