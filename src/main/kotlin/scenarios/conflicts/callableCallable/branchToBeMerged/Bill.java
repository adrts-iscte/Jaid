package scenarios.conflicts.callableCallable.branchToBeMerged;

//e17675b0-a87e-44c0-8482-12caf6d55aea
public class Bill {

    //70a4aa78-8aa2-41f7-b677-db32260c9807
    void differentModificationsOfParameters() {}

    //3c3103a6-ad3a-424c-8d9c-2e4de4d74915
    void clcBl2() {
        //code to calculate bills
    }

    //3c3103a6-ad3a-424c-8d9c-2e4de4d749a4
    void clcBl() {
        //code to calculate bills
        //return
    }

    //84ad2261-92da-476a-9470-182e7ac64b08
    void subOfBills() {
        clcBl();
    }

    //ef6cc861-1697-4899-b3b5-d595c4e7c11b
    void subOfBills(int param1) {
        clcBl();
    }

    //422d2319-828a-47e6-8365-ce6d1df1dcd1
    void productOfBills() {
        clcBl();
    }

    //9f5116db-0750-47e1-b96c-4e8979441fb8
    void divisionOfBills() {}

    //98914feb-2c16-461d-8854-1892b600a767
    void calculateBill() {
        clcBl();
    }

    //00155fad-0118-4bb1-9695-47986ef27c97
    void methodWithSameNameAndDifferentParameters(boolean param1) {}

    //5c6f2c92-328f-44d6-881e-21e24a8b885a
    void methodWithSameNameAndDifferentParameters(float param1) {}

    //27d38d68-917c-44a9-8034-f6c3f5685df6
    void methodToBeCompared(int param1) {}

    //0a9c0bbf-8047-4778-b6d3-9e7e00d0ef65
    void methodToBeCompared(double param1) {}

    //0c6abd2a-a1b7-4791-8e6f-f82858f6c623
    int methodReturnTypeChanged() { return 1; }

    //1c699b59-6cd1-44a2-98c6-086b2b948d28
    void methodToBeRenamed1() { }

    //cc227ee7-3de3-464c-9979-c60e743abc26
    void methodToBeRenamedAndSameSignature(int param1) { }

    //12296537-d38d-46f6-98d4-36a41d0b5250
    void methodToBeRenamedAndDifferentSignature(int param1) { }

    //6ab885ef-9912-428f-9ac9-cc1b86dfa7f2
    public final static void methodToModifiersChangedConflictious() { }

    //1e6bbf4d-a099-491c-8a5e-c805446cc486
    private final static void methodToModifiersChangedNotConflictious() { }
}
