package com.zq.jdk8;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class PrimeCollector implements Collector<Integer, List<Integer>, List<Integer>> {

    public static final Set<Characteristics> CH_CONCURRENT_ID
            = Collections.unmodifiableSet(EnumSet.of(Characteristics.CONCURRENT,
            Characteristics.UNORDERED,
            Characteristics.IDENTITY_FINISH));
    public static final Set<Characteristics> CH_CONCURRENT_NOID
            = Collections.unmodifiableSet(EnumSet.of(Characteristics.CONCURRENT,
            Characteristics.UNORDERED));
    public static final Set<Characteristics> CH_ID
            = Collections.unmodifiableSet(EnumSet.of(Characteristics.IDENTITY_FINISH));
    public static final Set<Characteristics> CH_UNORDERED_NOID
            = Collections.unmodifiableSet(EnumSet.of(Characteristics.UNORDERED));
    public static final Set<Characteristics> CH_UNORDERED_ID
            = Collections.unmodifiableSet(EnumSet.of(Characteristics.UNORDERED,
            Characteristics.IDENTITY_FINISH));
    public static final Set<Characteristics> CH_NOID = Collections.emptySet();

    private Supplier<List<Integer>> supplier;

    private BiConsumer<List<Integer>, Integer> accumulator;

    private BinaryOperator<List<Integer>> combiner;

    private Function<List<Integer>, List<Integer>> finisher;

    private Set<Characteristics> characteristics;

    private static final int PRIME_SIFT_TABLE_SIZE = 100000;

    private static final boolean[] PRIME_SIFT_TABLE;

    static {

        PRIME_SIFT_TABLE = new boolean[PRIME_SIFT_TABLE_SIZE + 1];
        // 1 is not a prime num
        Arrays.fill(PRIME_SIFT_TABLE, 2, PRIME_SIFT_TABLE_SIZE + 1, true);

        int border = (int) Math.sqrt(Integer.MAX_VALUE);
        for (int i = 2; i <= PRIME_SIFT_TABLE_SIZE; i++) {
            if (PRIME_SIFT_TABLE[i] && i <= border) { // i is a prime num
                for (int j = i * i; j <= PRIME_SIFT_TABLE_SIZE; j += i) {
                    PRIME_SIFT_TABLE[j] = false;
                }
            }
        }

    }

    public static boolean isPrime(int val) {
        if (val <= 0) return false;

        if (val <= PRIME_SIFT_TABLE_SIZE) return PRIME_SIFT_TABLE[val];

        int t = (int) Math.sqrt(val);
        for (int i = 2; i <= t; i++) {
            if (val % i == 0)
                return false;
        }
        return true;
    }

    public PrimeCollector(Set<Characteristics> characteristics) {
        this.supplier = ArrayList::new;
        this.accumulator = (r, t) -> {
            if (isPrime(t))
                r.add(t);
        };
        this.combiner = (r1, r2) -> {
            r1.addAll(r2);
            return r1;
        };
        this.finisher = (t) -> {
            throw new UnsupportedOperationException(
                    "under the identity mode, this func should not be called.");
        };
        this.characteristics = characteristics;
    }

    @Override
    public Supplier<List<Integer>> supplier() {
        return supplier;
    }

    @Override
    public BiConsumer<List<Integer>, Integer> accumulator() {
        return accumulator;
    }

    @Override
    public BinaryOperator<List<Integer>> combiner() {
        return combiner;
    }

    @Override
    public Function<List<Integer>, List<Integer>> finisher() {
        return finisher;
    }

    @Override
    public Set<Characteristics> characteristics() {
        return characteristics;
    }
}

class Set2MapCollector<T> implements Collector<T, Set<T>, Map<T, T>> {

    private Set<Characteristics> characteristics;

    public Set2MapCollector() {
        this(Collections.unmodifiableSet(EnumSet.of(Characteristics.IDENTITY_FINISH)));
    }

    public Set2MapCollector(Set<Characteristics> cs) {
        characteristics = Objects.requireNonNull(cs);
    }

    @Override
    public Supplier<Set<T>> supplier() {
        System.out.println("supplier() is called: " + Thread.currentThread().getName());
        return () -> {
            System.out.println("supplier().get() is called: " + Thread.currentThread().getName());
            return new HashSet<>();
        };
    }

    @Override
    public BiConsumer<Set<T>, T> accumulator() {
        System.out.println("accumulator() is called: " + Thread.currentThread().getName());
        return (a, t) -> {
            System.out.println("accumulator().accept() is called: " + Thread.currentThread().getName());
            a.add(t);
        };
    }

    @Override
    public BinaryOperator<Set<T>> combiner() {
        System.out.println("combiner() is called: " + Thread.currentThread().getName());
        return (s1, s2) -> {
            System.out.println("combiner().apply() is called: " + Thread.currentThread().getName());
            s1.addAll(s2);
            return s1;
        };
    }

    @Override
    public Function<Set<T>, Map<T, T>> finisher() {
        System.out.println("finisher: " + Thread.currentThread().getName());
        return Objects.requireNonNull(characteristics).contains(Characteristics.IDENTITY_FINISH) ?
                (s) -> {
                    throw new UnsupportedOperationException(
                            "under the identity mode, this func should not be called.");
                } :
                (s) -> s.stream().collect(Collectors.toMap(Function.identity(), Function.identity()));
    }

    @Override
    public Set<Characteristics> characteristics() {
        System.out.println("characteristics: " + Thread.currentThread().getName());
        return characteristics;
    }
}
