//af8d6e02-1788-4ef8-b6d8-065bb5eb75af
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
    void methodToBeRenamed(double param1) {
        ToBeStaticBill.staticBillMethod();
    }

    //84ad2261-92da-476a-9470-182e7ac64b08
    void methodBodyChanged() {
        System.out.println(value);
        methodToBeRenamed(value);
        methodToBeRenamed(value);
        Bill newBill = bill.bill;
    }

}

//7b5478bd-bfb2-4755-b960-c3d4d944aebc
class ToBeStaticBill {

    //7d99b222-35be-4398-bd78-a00dd16c60e8
    public static void staticBillMethod() {

    }
}
