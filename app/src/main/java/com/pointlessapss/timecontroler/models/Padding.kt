package com.pointlessapss.timecontroler.models

class Padding(var top: Int = 0, var bottom: Int = 0, var left: Int = 0, var right: Int = 0) {
	val horizontal: Int
		get() = left + right
	val vertical: Int
		get() = top + bottom

	constructor(padding: Int = 0) : this(padding, padding, padding, padding)
	constructor(horizontalPadding: Int = 0, verticalPadding: Int = 0) : this(
		verticalPadding,
		verticalPadding,
		horizontalPadding,
		horizontalPadding
	)
}