package fr.ens.biologie.genomique.kenetre.bio;

public class SparseExpressionMatrixTest extends AbstractExpressionMatrixTest {

  @Override
  protected ExpressionMatrix createMatrix() {

    return new SparseExpressionMatrix();
  }

  @Override
  protected ExpressionMatrix createMatrix(double defaultValue) {

    return new SparseExpressionMatrix(defaultValue);
  }
}
