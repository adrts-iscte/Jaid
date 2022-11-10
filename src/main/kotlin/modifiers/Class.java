package modifiers;

//a0913ef3-3cec-457c-b53d-7b2f3b71c36e
class ClassTest {

    private final static int methodToBeRemoved(int a, boolean b) {
        return 1;
    }

    private synchronized int methodToBeAdded(int a, boolean b) {
        return 1;
    }
}