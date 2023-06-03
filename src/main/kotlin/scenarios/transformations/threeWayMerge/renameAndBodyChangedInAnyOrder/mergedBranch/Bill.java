//39e49da4-f96e-49d5-845c-067c47373c42
package scenarios.transformations.threeWayMerge.renameAndBodyChangedInAnyOrder.mergedBranch;

//e17675b0-a87e-44c0-8482-12caf6d55aea
class Bill {

    //3c3103a6-ad3a-424c-8d9c-2e4de4d749a4
    void methodToBeRenamed(double param1) {}

    //84ad2261-92da-476a-9470-182e7ac64b08
    void methodBodyChanged() {
        methodToBeRenamed(1);
        methodToBeRenamed(2);
    }

}
