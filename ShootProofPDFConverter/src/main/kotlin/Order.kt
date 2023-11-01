class Order(orderNumber: Int, packageCodes: MutableMap<String, String>){

    val orderNumber: Int = orderNumber
    var galleryName: String  = ""
    var nameBillingAddress: String  = ""
    var fileOrdered: String  = ""
    var sizeOrdered: String = ""
    private var packageCode: String? = ""
    var quantity: Int = 0
    var totalPrice: String = ""
    private var aPackage: String  = ""

    val packageCodes: MutableMap<String, String> = packageCodes
    fun convertToCSV(): String{
        packageCode = packageCodes[sizeOrdered.lowercase()]
        aPackage = packageCode.plus("-$quantity")
        return "$orderNumber,$galleryName,$nameBillingAddress,$fileOrdered,$sizeOrdered,$packageCode,$quantity,$totalPrice,$aPackage\n"
    }

    fun copyOrder(): Order{
        var newOrder: Order = Order(this.orderNumber, packageCodes)
        newOrder.galleryName = this.galleryName
        newOrder.nameBillingAddress = this.nameBillingAddress
        return newOrder
    }
}