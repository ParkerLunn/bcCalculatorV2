
import org.antlr.v4.runtime.misc.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class EvalVisitor extends CalculatorBaseVisitor<Value> {

    // used to compare floating point numbers
    public static final double SMALL_VALUE = 0.00000000001;

    // store variables (there's only one global scope!)
    private Map<String, Value> memory = new HashMap<String, Value>();

    // assignment/id overrides
    @Override
    public Value visitAssignment(CalculatorParser.AssignmentContext ctx) {
        String id = ctx.ID().getText();
        Value value = this.visit(ctx.expr());
        return memory.put(id, value);
    }

    @Override
    public Value visitIdAtom(CalculatorParser.IdAtomContext ctx) {
        String id = ctx.getText();
        Value value = memory.get(id);
        if(value == null) {
            throw new RuntimeException("no such variable: " + id);
        }
        return value;
    }

    // atom overrides
    @Override
    public Value visitStringAtom(CalculatorParser.StringAtomContext ctx) {
        String str = ctx.getText();
        // strip quotes
        str = str.substring(1, str.length() - 1).replace("\"\"", "\"");
        return new Value(str);
    }

    @Override
    public Value visitNumberAtom(CalculatorParser.NumberAtomContext ctx) {
        return new Value(Double.valueOf(ctx.getText()));
    }

    @Override
    public Value visitBooleanAtom(CalculatorParser.BooleanAtomContext ctx) {
        return new Value(Boolean.valueOf(ctx.getText()));
    }

    @Override
    public Value visitNilAtom(CalculatorParser.NilAtomContext ctx) {
        return new Value(null);
    }

    // expr overrides
    @Override
    public Value visitParExpr(CalculatorParser.ParExprContext ctx) {
        return this.visit(ctx.expr());
    }

    @Override
    public Value visitPowExpr(CalculatorParser.PowExprContext ctx) {
        Value left = this.visit(ctx.expr(0));
        Value right = this.visit(ctx.expr(1));
        Value val = new Value(Math.pow(left.asDouble(), right.asDouble()));
        return val;
    }

    @Override
    public Value visitUnaryMinusExpr(CalculatorParser.UnaryMinusExprContext ctx) {
        Value value = this.visit(ctx.expr());
        return new Value(-value.asDouble());
    }

    @Override
    public Value visitNotExpr(CalculatorParser.NotExprContext ctx) {
        Value value = this.visit(ctx.expr());
        return new Value(!value.asBoolean());
    }

    @Override 
    public Value visitPreIncExpr(CalculatorParser.PreIncExprContext ctx) {
        String id = ctx.ID().getText();
        Value value = memory.get(id);
        memory.put(id, new Value(value.asDouble()+1));
        return new Value(value.asDouble()+1);
    }

    @Override 
    public Value visitPostIncExpr(CalculatorParser.PostIncExprContext ctx) {
        String id = ctx.ID().getText();
        Value value = memory.get(id);
        memory.put(id, new Value(value.asDouble()+1));
        return new Value(value.asDouble());
    }

    @Override 
    public Value visitPreDecExpr(CalculatorParser.PreDecExprContext ctx) {
        String id = ctx.ID().getText();
        Value value = memory.get(id);
        memory.put(id, new Value(value.asDouble()-1));
        return new Value(value.asDouble()-1);
    }

    @Override 
    public Value visitPostDecExpr(CalculatorParser.PostDecExprContext ctx) {
        String id = ctx.ID().getText();
        Value value = memory.get(id);
        memory.put(id, new Value(value.asDouble()-1));
        return new Value(value.asDouble());
    }

    @Override
    public Value visitMultiplicationExpr(@NotNull CalculatorParser.MultiplicationExprContext ctx) {

        Value left = this.visit(ctx.expr(0));
        Value right = this.visit(ctx.expr(1));
        Value val;
        switch (ctx.op.getType()) {
            case CalculatorParser.MULT:
                val = new Value(left.asDouble() * right.asDouble());
                return val;
            case CalculatorParser.DIV:
                val = new Value(left.asDouble() / right.asDouble());
                return val;
            case CalculatorParser.MOD:
                val = new Value(left.asDouble() % right.asDouble());
                return val;
            default:
                throw new RuntimeException("unknown operator: " + CalculatorParser.tokenNames[ctx.op.getType()]);
        }
    }

    @Override
    public Value visitAdditiveExpr(@NotNull CalculatorParser.AdditiveExprContext ctx) {

        Value left = this.visit(ctx.expr(0));
        Value right = this.visit(ctx.expr(1));
        Value val;
        switch (ctx.op.getType()) {
            case CalculatorParser.PLUS:
                val = left.isDouble() && right.isDouble() ?
                        new Value(left.asDouble() + right.asDouble()) :
                        new Value(left.asString() + right.asString());
                return val;
            case CalculatorParser.MINUS:
                val = new Value(left.asDouble() - right.asDouble());
                return val;
            default:
                throw new RuntimeException("unknown operator: " + CalculatorParser.tokenNames[ctx.op.getType()]);
        }
    }

    @Override
    public Value visitRelationalExpr(@NotNull CalculatorParser.RelationalExprContext ctx) {

        Value left = this.visit(ctx.expr(0));
        Value right = this.visit(ctx.expr(1));
        Value val;

        switch (ctx.op.getType()) {
            case CalculatorParser.LT:
                return new Value(left.asDouble() < right.asDouble());
            case CalculatorParser.LTEQ:
                return new Value(left.asDouble() <= right.asDouble());
            case CalculatorParser.GT:
                return new Value(left.asDouble() > right.asDouble());
            case CalculatorParser.GTEQ:
                return new Value(left.asDouble() >= right.asDouble());
            default:
                throw new RuntimeException("unknown operator: " + CalculatorParser.tokenNames[ctx.op.getType()]);
        }
    }

    @Override
    public Value visitEqualityExpr(@NotNull CalculatorParser.EqualityExprContext ctx) {

        Value left = this.visit(ctx.expr(0));
        Value right = this.visit(ctx.expr(1));

        switch (ctx.op.getType()) {
            case CalculatorParser.EQ:
                return left.isDouble() && right.isDouble() ?
                        new Value(Math.abs(left.asDouble() - right.asDouble()) < SMALL_VALUE) :
                        new Value(left.equals(right));
            case CalculatorParser.NEQ:
                return left.isDouble() && right.isDouble() ?
                        new Value(Math.abs(left.asDouble() - right.asDouble()) >= SMALL_VALUE) :
                        new Value(!left.equals(right));
            default:
                throw new RuntimeException("unknown operator: " + CalculatorParser.tokenNames[ctx.op.getType()]);
        }
    }

    @Override
    public Value visitLibFuncExpr(@NotNull CalculatorParser.LibFuncExprContext ctx){
        Value value = this.visit(ctx.expr());

        switch (ctx.op.getType()){
            case CalculatorParser.SIN:
                return new Value(Math.sin(value.asDouble()));
            case CalculatorParser.COS:
                return new Value(Math.cos(value.asDouble()));
            case CalculatorParser.LOG:
                return new Value(Math.log(value.asDouble()));
            case CalculatorParser.EXP:
                return new Value(Math.exp(value.asDouble()));
            case CalculatorParser.SQRT:
                return new Value(Math.sqrt(value.asDouble()));
            case CalculatorParser.READ:
                Scanner in = new Scanner(System.in);
                return new Value(in.nextDouble());
            default:
                throw new RuntimeException("unknown operator: " + CalculatorParser.tokenNames[ctx.op.getType()]);

        }
    }

    @Override
    public Value visitAndExpr(CalculatorParser.AndExprContext ctx) {
        Value left = this.visit(ctx.expr(0));
        Value right = this.visit(ctx.expr(1));
        return new Value(left.asBoolean() && right.asBoolean());
    }

    @Override
    public Value visitOrExpr(CalculatorParser.OrExprContext ctx) {
        Value left = this.visit(ctx.expr(0));
        Value right = this.visit(ctx.expr(1));
        return new Value(left.asBoolean() || right.asBoolean());
    }

    // log override
    @Override
    public Value visitPrint(CalculatorParser.PrintContext ctx) {

        List<CalculatorParser.ExprContext> exprList = ctx.expr();

        for(CalculatorParser.ExprContext eCtx: exprList){

            Value value = this.visit(eCtx);

            if(!value.isBoolean())
                System.out.println(value);
            else
                System.out.println(value.asBoolean()?"1":"0");
        }

        return Value.VOID;
    }

    // if override
    @Override
    public Value visitIf_stat(CalculatorParser.If_statContext ctx) {

        List<CalculatorParser.Condition_blockContext> conditions =  ctx.condition_block();

        boolean evaluatedBlock = false;

        for(CalculatorParser.Condition_blockContext condition : conditions) {

            Value evaluated = this.visit(condition.expr());

            if(evaluated.asBoolean()) {
                evaluatedBlock = true;
                // evaluate this block whose expr==true
                this.visit(condition.stat_block());
                break;
            }
        }

        if(!evaluatedBlock && ctx.stat_block() != null) {
            // evaluate the else-stat_block (if present == not null)
            this.visit(ctx.stat_block());
        }

        return Value.VOID;
    }

    // while override
    @Override
    public Value visitWhile_stat(CalculatorParser.While_statContext ctx) {

        Value value = this.visit(ctx.expr());

        while(value.asBoolean()) {

            // evaluate the code block
            this.visit(ctx.stat_block());

            // evaluate the expression
            value = this.visit(ctx.expr());
        }

        return Value.VOID;
    }

    @Override
    public Value visitFor_stat(CalculatorParser.For_statContext ctx){
        this.visit(ctx.assignment());
        Value cond = this.visit(ctx.expr(0));

        while(cond.asBoolean()){

            this.visit(ctx.stat_block());

            this.visit(ctx.expr(1));
            cond=this.visit(ctx.expr(0));
        }

    return Value.VOID;
    }
}