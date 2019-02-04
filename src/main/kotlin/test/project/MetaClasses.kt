package test.project

import java.util.*

sealed class Expr

data class FieldRef(val path: String) : Expr()

data class Concat(val expr1: Expr, val expr2: Expr) : Expr()

data class Const(val value: Any) : Expr()

data class Compare(val expr1: Expr, val expr2: Expr) : Expr()

data class ProportionOf(val field: FieldRef, val comparision: Compare) : Expr()




data class FieldDef(val value: Expr?, val from: Expr?, val to: Expr?)

fun test() {

    FieldRef("workitem.status")

    ProportionOf(FieldRef("workitems"), Compare(FieldRef("workitems.status"), Const("closed")))

    var x: Expr = FieldRef("")

    when (x) {
        is FieldRef -> println(x.path)
    }

}

elComplDegree() {

    FieldRef("complDegree")

    ProportionOf(FieldRef("workitems"), Compare(FieldRef("workitems.status"), Const("closed")))




}


elClosingDate() {

    FieldRef("complDegree")

    Conditional(
        And(
            IfEmpty("closeDate"),
            AllOf(FieldRef("workitems"), Compare(FieldRef("workitems.status"), Const("closed")))
        ),
        NowDate()
    )



    ReqDate() {
        fromHint = FieldRef(....)
    }




}

interface Constraint {
    val from: Expr
    val to: Expr
}

interface ItemDefinition {

    val value: Expr
    val constrant: Constraint
    val hint: Constraint
    val editable: Expr
    val type: Type


}

/*

{
    "phases": [
    {
        "name": "Phase 1",
        "elements": [
        {
            "name": "Element 1",
            "closingDate": "2019-01-30"
        }
        ]
    }
    ],

    "rules": [
        "Phase 1": [
            "name": {
                "editable": "Const(true)"
            },
            "elements": {
                "Element 1": {
                    "closingDate": {
                        "value": "Conditional(........)"
                        "editable": ...
                        "from": Expr
                    }
                }
            }
        ]
    ]


}


 */

fun externalize(expr: Expr): String = when (expr) {
    is FieldRef -> "FieldRef('${expr.path}')"
    is Concat -> "Concat(${externalize(expr.expr1)}, ${externalize(expr.expr2)})"
    is Compare -> "Compare(${externalize(expr.expr1)}, ${externalize(expr.expr2)})"
    else -> throw IllegalStateException()
}

interface EvalContext {
    fun getValue(path: String): Optional<Any?>
    fun subItems(path: String): List<EvalContext>
}

class PhaseModelContext(prefix: String, val dto: PhaseDto): EvalContext {

    override fun getValue(path: String): Any? = when(path) {
        "complete" -> dto.complete
        else -> throw IllegalStateException()
    }
    override fun subItems(path: String): List<EvalContext> = when(path) {
        "elements" -> dto.elements.map { ElementModelContext("elemnents", it, dto) }
        else -> throw IllegalStateException()
    }
}

class ElementModelContext(prefix: String, val dto: ElementDto, parent: PhaseDto): EvalContext {
    override fun getValue(path: String): Any? = when(path) {
        "startDate" -> dto.startDate
        "endDate" -> dto.endDate
        else -> throw IllegalStateException()
    }
    override fun subItems(path: String): List<EvalContext> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}

fun evaluate(ctx: EvalContext, expr: Expr): Any? = when (expr) {
    is FieldRef -> ctx.getValue(expr.path)
    is Concat -> evaluate(ctx, expr.expr1).toString() + evaluate(ctx, expr.expr2).toString()
    is Const -> expr.value
    is ProportionOf -> {
        val subItemsCtx = ctx.subItems(expr.field.path)
        val matchingCount = subItemsCtx.filter { evaluate(it, expr.comparision) as Boolean }.size
        matchingCount.toDouble() / subItemsCtx.size.toDouble()
    }
    else -> throw IllegalStateException()
}

fun dependencies(expr: Expr): List<FieldRef> = when (expr) {
    is FieldRef -> listOf(expr)
    is Const -> listOf()
    is Concat ->  dependencies(expr.expr1) + dependencies(expr.expr2)
    is Compare -> dependencies(expr.expr1) + dependencies(expr.expr2)
    is ProportionOf -> listOf(expr.field) + dependencies(expr.comparision)
}