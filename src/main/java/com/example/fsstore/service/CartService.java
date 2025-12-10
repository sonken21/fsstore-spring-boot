package com.example.fsstore.service;

import com.example.fsstore.entity.Cart;
import com.example.fsstore.entity.CartItem;
import com.example.fsstore.entity.Product;
import com.example.fsstore.repository.CartItemRepository;
import com.example.fsstore.repository.CartRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductService productService; // Giả sử đã tồn tại

    public CartService(CartRepository cartRepository, CartItemRepository cartItemRepository, ProductService productService) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productService = productService;
    }

    // Phương thức lấy giỏ hàng hiện tại hoặc tạo mới
    @Transactional
    public Cart getOrCreateCart(Long cartId) {
        if (cartId != null) {
            Optional<Cart> existingCart = cartRepository.findById(cartId);
            if (existingCart.isPresent()) {
                return existingCart.get();
            }
        }
        // Tạo giỏ hàng mới
        Cart newCart = new Cart();
        return cartRepository.save(newCart);
    }

    /**
     * Thêm sản phẩm vào giỏ hàng với logic kiểm tra tồn kho chính xác.
     * @return true nếu thêm thành công, false nếu số lượng vượt quá tồn kho.
     */
    @Transactional
    public boolean addProductToCart(Cart cart, Long productId, int quantity) {
        if (quantity <= 0) return true; // Không cần làm gì nếu quantity không hợp lệ

        // 1. Lấy thông tin sản phẩm và kiểm tra tồn kho ban đầu
        Product product = productService.findProductById(productId);
        if (product == null) {
            throw new IllegalArgumentException("Sản phẩm không tồn tại.");
        }

        // Tồn kho hiện tại của sản phẩm
        int productStock = product.getStock();

        // 2. Kiểm tra xem mặt hàng đã có trong giỏ hàng chưa
        Optional<CartItem> existingItemOptional = cartItemRepository.findByCartAndProductId(cart, productId);

        int currentQuantityInCart = 0;
        CartItem cartItem;

        if (existingItemOptional.isPresent()) {
            // Đã có: Cập nhật số lượng
            cartItem = existingItemOptional.get();
            currentQuantityInCart = cartItem.getQuantity();

        } else {
            // Chưa có: Tạo mặt hàng mới
            cartItem = new CartItem();
            cartItem.setCart(cart);
            cartItem.setProduct(product);
            // Quan trọng: Cần thêm cartItem vào collection của Cart
            cart.getItems().add(cartItem);
        }

        // 3. Tính tổng số lượng sau khi thêm
        int totalQuantityAfterAddition = currentQuantityInCart + quantity;

        // 4. KIỂM TRA LOGIC TỒN KHO: So sánh tổng số lượng mới với tồn kho
        if (totalQuantityAfterAddition > productStock) {
            // Nếu vượt quá tồn kho, trả về lỗi
            return false;
        }

        // 5. Cập nhật số lượng và lưu
        cartItem.setQuantity(totalQuantityAfterAddition);
        cartItemRepository.save(cartItem);
        cartRepository.save(cart); // Lưu Cart để cập nhật items collection (tùy chọn)

        return true;
    }
}