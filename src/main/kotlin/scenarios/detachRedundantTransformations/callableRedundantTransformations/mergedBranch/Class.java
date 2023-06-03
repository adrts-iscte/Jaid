//2a55a277-4941-40e5-876f-0cb7a8abaec0
package scenarios.detachRedundantTransformations.callableRedundantTransformations.mergedBranch;

//76b74b0a-d31f-4fbc-a837-048474fd7771
class Class {

    //ef23ea73-6ee1-460e-87e1-86bb532fb8c6
    void method() {}

    //6014eb44-12e7-42f4-a81a-8f2563581880
    void newMethod() {}

    //60d6c985-b76c-464f-9ae9-3b089fb0a4be
    private void methodBodyChangedEqualBodies() {
        method();
    }

    //ee3cb546-23cf-4284-b1af-6dff9f2ae278
    public int methodBodyChangedDifferentBodies() {
        return 2;
    }

    //60d6c985-b76c-464f-9ae9-3b089fb0a4dd
    void methodSignatureChangedEqual() {}

    //60d6c985-b76c-464f-9ae9-3b089fb0a4ee
    void methodSignatureChangedToDifferent2() {}
}
