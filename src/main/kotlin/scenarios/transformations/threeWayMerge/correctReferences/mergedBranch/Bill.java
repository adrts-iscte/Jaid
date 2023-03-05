package scenarios.transformations.threeWayMerge.correctReferences.mergedBranch;

//e17675b0-a87e-44c0-8482-12caf6d55aea
class Bill {

    //5dfa5475-cb03-4bde-8c0b-94030344a150
    private Bill bill = new Bill();

    //4413f187-65d4-49ea-8ff6-66201fa43c2d
    public Bill() {}

    //3c3103a6-ad3a-424c-8d9c-2e4de4d749d2
    private double value;

    //3c3103a6-ad3a-424c-8d9c-2e4de4d749a4
    void methodToBeRenamed(double param1) {}

    //84ad2261-92da-476a-9470-182e7ac64b08
    void methodBodyChanged() {
        System.out.println(value);
        methodToBeRenamed(value);
        methodToBeRenamed(value);
        Bill newBill = bill.bill;
    }

}
