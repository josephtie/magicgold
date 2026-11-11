/// Modèles de commande (alignés sur l'API backend /api/v1/orders).
class OrderItemRequest {
  const OrderItemRequest({required this.productId, required this.quantity});

  final String productId;
  final int quantity;

  Map<String, dynamic> toJson() => {
        'productId': productId,
        'quantity': quantity,
      };
}

class OrderRequest {
  const OrderRequest({required this.items, this.discountAmount = 0});

  final List<OrderItemRequest> items;
  final double discountAmount;

  Map<String, dynamic> toJson() => {
        'discountAmount': discountAmount,
        'items': items.map((e) => e.toJson()).toList(),
      };

  factory OrderRequest.fromJson(Map<String, dynamic> json) => OrderRequest(
        discountAmount: (json['discountAmount'] as num?)?.toDouble() ?? 0,
        items: (json['items'] as List<dynamic>)
            .map((e) => OrderItemRequest(
                  productId: (e as Map<String, dynamic>)['productId'] as String,
                  quantity: (e['quantity'] as num).toInt(),
                ))
            .toList(),
      );
}
