package scenarios.javaDocTransformations.base;

//d0779f95-d537-4501-b708-fc50747e6616
class ClassWithJavaDoc {

    //83dba2c8-a01d-4352-b8df-794ab8b44e3a
    private ClassWithJavaDoc(int javadocAdded) {

    }

    /**
     * @param javadocRemoved
     * 35c01991-470c-4bee-873f-53cd3372bd19
     */
    private ClassWithJavaDoc(int asd, int javadocRemoved) {

    }

    /**
     * @param javadocModified
     * 473f01ad-f414-4d4c-b216-1f18140aa3e5
     */
    private ClassWithJavaDoc(int asd, int dsa, int javadocModified) {

    }

    /*
      Method
      dd35e588-8a52-4b6e-a084-0f46049da9ad
     */
    private void methodToHaveJavadocAdded(int param) {

    }

    /**
     *
     * @param param
     * dd35e588-8a52-4b6e-a084-0f46049da9ef
     */
    private void methodToHaveJavadocRemoved(int param) {

    }

    /**
     *
     * @param param
     * dd35e588-8a52-4b6e-b085-0f46049da9ad
     */
    private void methodToHaveJavadocChanged(int param) {

    }

}

/**
 *  class
 *  13c9f311-0d07-46aa-8591-ef22c6ab8e49
 */
class ClassWithoutJavaDoc {

}

/**
 *  class
 *  13c9f311-0d07-46aa-8591-ef22c6ab8e42
 */
class ClassWithJavaDocModified {

}