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
    void methodRenamed(double param1) {
        StaticBill.staticBillMethod();
    }

    //84ad2261-92da-476a-9470-182e7ac64b08
    void methodBodyChanged() {
        System.out.println(renamedValue);
        methodRenamed(renamedValue);
        methodRenamed(renamedValue);
        RenamedBill newBill = renamedBill.renamedBill;
    }
}

//7b5478bd-bfb2-4755-b960-c3d4d944aebc
class StaticBill {

    //7d99b222-35be-4398-bd78-a00dd16c60e8
    public static void staticBillMethod() {

    }
}

//a8c578c6-789f-44f7-b7db-be60f3ff5d53
class classToBeAdded {

    //f9889814-a802-4af3-9dcf-a147eb1f6af9
    void method() {
        StaticBill.staticBillMethod();
    }

}
