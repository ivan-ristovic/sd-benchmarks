package net.ristovic.benchmarks.osd.data.gen;

import java.util.Random;

public abstract class DataGenerator<T> extends ValueDataGenerator {

    protected DataGenerator(Random rng) {
        super(rng);
    }

    public abstract void generateFields(T obj);

}
