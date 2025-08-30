package piotrholda.portfoliomanager.corporateaction;

public interface CorporateActionVisitor {
    void visit(Dividend dividend);
    void visit(Split split);
}
