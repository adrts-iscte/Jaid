package scenarios.transformations.threeWayMerge.correctReferences.finalMergedVersion;

//e17675b0-a87e-44c0-8482-12caf6d55aea
class RenamedBill {

    //5dfa5475-cb03-4bde-8c0b-94030344a150
    private RenamedBill renamedBill = new RenamedBill();

    //4413f187-65d4-49ea-8ff6-66201fa43c2d
    public RenamedBill() {}

    //3c3103a6-ad3a-424c-8d9c-2e4de4d749d2
    private double renamedValue;

    //3c3103a6-ad3a-424c-8d9c-2e4de4d749a4
    void methodRenamed(double param1) {}

    //84ad2261-92da-476a-9470-182e7ac64b08
    void methodBodyChanged() {
        System.out.println(renamedValue);
        methodRenamed(renamedValue);
        methodRenamed(renamedValue);
        RenamedBill newBill = renamedBill.renamedBill;
    }
}
