/// Modèle produit (aligné sur l'entité Product du backend).
class Product {
  const Product({
    required this.id,
    required this.name,
    required this.unitPrice,
    required this.currency,
  });

  final String id;
  final String name;
  final double unitPrice;
  final String currency;

  factory Product.fromJson(Map<String, dynamic> json) => Product(
        id: json['id'] as String,
        name: json['name'] as String,
        unitPrice: (json['unitPrice'] as num).toDouble(),
        currency: json['currency'] as String? ?? 'XOF',
      );
}
