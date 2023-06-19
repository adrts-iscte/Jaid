//2a55a277-4941-40e5-876f-0cb7a8abaec0
package scenarios.detachRedundantTransformations.fileRedundantTransformations.mergedBranch;

import java.util.List;

//76b74b0a-d31f-4fbc-a837-048474fd7771
interface Class {

}

//a29fff32-f3bc-45fe-8f75-3c9c0f4ffceb
class ToBeAdded {

    //119c57af-0f3c-458c-acb1-e1efb6e42785
    double attribute;

    //40d21e24-a72c-488a-a2a2-82f347b44148
    static void method() {}
}

//3fd23723-5be9-4369-ba1d-35c8107eafc6
final class Renamed extends ToBeAdded implements Class {

    //1a878baf-9ca3-4c9b-83d5-a4269f07168f
    void RenamedMethod() {
        ToBeAdded.method();
    }

}