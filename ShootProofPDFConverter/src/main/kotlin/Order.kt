class Order(orderNumber: Int){

    val orderNumber: Int = orderNumber
    var galleryName: String  = ""
    var nameBillingAddress: String  = ""
    var fileOrdered: String  = ""
    var sizeOrdered: String = ""
    private var packageCode: String? = ""
    var quantity: Int = 0
    var totalPrice: String = ""
    var aPackage: String  = ""

    val packageCodes = mapOf("2.5 x 3.5 (8 Wallets)" to "A", "4 x 6" to "B", "5 x 7" to "C", "8 x 10" to "D", "11 x 14" to "E", "All Photos from this album" to "F", "Single Photo" to "G")

    fun convertToCSV(): String{
        packageCode = packageCodes[sizeOrdered]
        return "$orderNumber,$galleryName,$nameBillingAddress,$fileOrdered,$sizeOrdered,$packageCode,$quantity,$totalPrice,$aPackage\n"
    }

    fun incrementOrder(): Order{
        var newOrder: Order = Order(this.orderNumber+1)
        newOrder.galleryName = this.galleryName
        newOrder.nameBillingAddress = this.nameBillingAddress
        return newOrder
    }
}