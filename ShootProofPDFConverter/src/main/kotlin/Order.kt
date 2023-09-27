class Order(orderNumber: Int){

    val orderNumber = orderNumber
    var galleryName = ""
    var nameBillingAddress = ""
    var fileOrdered = ""
    var sizeOrdered = ""
    private var packageCode = ""
    var quantity = 0
    var totalPrice = 0
    var aPackage = ""

    val packageCodes = mapOf("2.5 x 3.5 (8 Wallets)" to 'A', "4 X 6" to 'B', "5 X 7" to 'C', "8 X 10" to 'D', "11 X 14" to 'E', "All Photos from this album" to 'F', "Single Photo" to 'G')

    fun convertToCSV(){

    }

    fun cloneForFile(newFileOrdered: String): Order{
        var newOrder: Order = Order(this.orderNumber+1)
        newOrder.galleryName = this.galleryName
        newOrder.nameBillingAddress = this.nameBillingAddress
        newOrder.fileOrdered = newFileOrdered
        return newOrder
    }
}