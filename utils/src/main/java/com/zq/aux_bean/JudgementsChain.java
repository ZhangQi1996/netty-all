package com.zq.aux_bean;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class JudgementsChain<T> {
    protected List<JudgementNode<T>> list = new LinkedList<>();

    public JudgementsChain<T> addJudgement(Predicate<T> predicate, Consumer<T> consumer) {
        return addJudgement(new JudgementNode<>(predicate, consumer));
    }

    public JudgementsChain<T> addJudgement(JudgementNode<T> judgementNode) {
        list.add(judgementNode);
        return this;
    }

    public void run(T t) {
        for (JudgementNode<T> judgementNode : list) {
            if (judgementNode.predicate.test(t)) {
                judgementNode.consumer.accept(t);
                break;
            }
        }
    }

    private JudgementsChain() {
    }

    public static <T> JudgementsChain<T> generateInstance() {
        return new JudgementsChain<>();
    }

    @SafeVarargs
    public static <T> JudgementsChain<T> generateInstance(JudgementNode<T>... ts) {
        JudgementsChain<T> ret = new JudgementsChain<>();
        for (JudgementNode<T> t : ts) {
            ret.addJudgement(t);
        }
        return ret;
    }

    public static class JudgementNode<T> {
        Predicate<T> predicate;
        Consumer<T> consumer;

        JudgementNode(Predicate<T> predicate, Consumer<T> consumer) {
            this.predicate = Objects.requireNonNull(predicate);
            this.consumer = Objects.requireNonNull(consumer);
        }
    }
}
