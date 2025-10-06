public class TestInputProvider implements InputProvider {
    private String testInput;

    public void setTestInput(String input) {
        this.testInput = input;
    }

    @Override
    public String getInput() {
        return testInput;
    }
}