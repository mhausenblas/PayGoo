package info.paygoo

object WireFormat
case object HTML extends WireFormat
case object JSON extends WireFormat
case object Text extends WireFormat
case object Turtle extends WireFormat

abstract class PayGoo(label: String) {
	def ser( format: WireFormat = JSON )
}

case class PayGooResource extends PayGoo {
	override def ser (format: WireFormat = JSON ) :Unit = {
		println(label)
	}
}

case class PayGooContainter extends PayGoo

object testme {
	val r = new PayGooResource

	def main(args: Array[String]) {
		r.ser
	}
}