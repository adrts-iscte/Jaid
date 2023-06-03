//2a55a277-4941-40e5-876f-0cb7a8abaec0
package scenarios.detachRedundantTransformations.fieldRedundantTransformations.branchToBeMerged;

//76b74b0a-d31f-4fbc-a837-048474fd7771
class Class {

    //ef23ea73-6ee1-460e-87e1-86bb532fb8c6
    int field;

    //9d3d8934-a193-4298-9aca-888fb6cfe255
    int newMethod;

    //60d6c985-b76c-464f-9ae9-3b089fb0a4be
    private double fieldBodyChangedEqualBodies = field;
    
    //ee3cb546-23cf-4284-b1af-6dff9f2ae278
    static int fieldBodyChangedDifferentBodies = 1;

    //60d6c985-b76c-464f-9ae9-3b089fb0a4dd
    int fieldSignatureChangedEqual;

    //60d6c985-b76c-464f-9ae9-3b089fb0a4ee
    int fieldSignatureChangedDifferent;
}
