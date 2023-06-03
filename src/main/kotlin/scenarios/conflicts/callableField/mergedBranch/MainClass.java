//9b529b89-4491-4267-8271-f73f354af466
package scenarios.conflicts.callableField.mergedBranch;

//ad2d8564-777f-4cea-a18c-2587596366e1
class MainClass {

    //6549b772-624b-44b3-a16b-d2d7530c8077
    int attribute;

    //2b760721-3b81-4389-952f-4dcc3da99a5a
    int method() {
        int twoAttributeValues = attribute + attribute;
        return twoAttributeValues;
    }

    //3d155879-d502-4245-a74b-4c542ff51174
    int intToBooleanAttribute;

    //ed65c167-a2d0-4574-b702-02dcfd0ee248
    int methodToUseIntToBooleanAttribute() {
        int twoAttributeValues = intToBooleanAttribute + intToBooleanAttribute;
        return twoAttributeValues;
    }
}
