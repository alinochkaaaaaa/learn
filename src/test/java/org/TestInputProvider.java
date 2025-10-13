package test.org;

import org.InputProvider;
import java.util.LinkedList;
import java.util.Queue;

public class TestInputProvider implements InputProvider {
    private Queue<String> inputQueue = new LinkedList<>();

    public void addInput(String input) {
        inputQueue.add(input);
    }

    @Override
    public String getInput() {
        return inputQueue.poll();
    }

    @Override
    public boolean hasInput() {
        return !inputQueue.isEmpty();
    }
}