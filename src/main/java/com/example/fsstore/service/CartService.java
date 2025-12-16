// src/main/java/com/example/fsstore/service/CartService.java

package com.example.fsstore.service;

import com.example.fsstore.entity.Cart;
import com.example.fsstore.entity.CartItem;
import com.example.fsstore.entity.Product;
import com.example.fsstore.repository.CartItemRepository;
import com.example.fsstore.repository.CartRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Optional;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductService productService;

    public CartService(CartRepository cartRepository, CartItemRepository cartItemRepository, ProductService productService) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productService = productService;
    }

    // Phương thức này không thay đổi
    @Transactional
    public Cart getOrCreateCart(Long cartId) {
        if (cartId != null) {
            // Sử dụng findByIdWithItems để luôn tải items (EAGER FETCH)
            Optional<Cart> existingCart = cartRepository.findByIdWithItems(cartId);
            if (existingCart.isPresent()) {
                System.out.println("DEBUG: [getOrCreateCart] Đã tìm thấy Cart ID: " + cartId);
                return existingCart.get();
            }
        }

        Cart newCart = new Cart();
        newCart = cartRepository.save(newCart);
        cartRepository.flush();
        System.out.println("DEBUG: [getOrCreateCart] Đã tạo Cart mới ID: " + newCart.getId());
        return newCart;
    }

    // Phương thức đã FIX lỗi dữ liệu price=NULL
    @Transactional
    public boolean addProductToCart(Cart cart, Long productId, int quantity) {
        if (quantity <= 0) {
            return false;
        }

        // ... (check null cart, giữ nguyên) ...

        // --- 1. Tìm Sản phẩm ---
        Optional<Product> productOptional = productService.findOptionalProductById(productId);

        if (!productOptional.isPresent()) {
            throw new IllegalArgumentException("Sản phẩm không tồn tại.");
        }

        Product product = productOptional.get();

        // ⭐ FIX VÀ XÁC NHẬN: Kiểm tra giá sản phẩm trước khi gán
        if (product.getPrice() == null) {
            throw new IllegalStateException("Lỗi dữ liệu: Giá sản phẩm ID " + productId + " là NULL. Vui lòng cập nhật giá.");
        }

        // --- 2. Tìm CartItem hiện có ---
        Optional<CartItem> existingItemOptional = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId);

        CartItem cartItem;

        if (existingItemOptional.isPresent()) {
            cartItem = existingItemOptional.get();
            cartItem.setQuantity(cartItem.getQuantity() + quantity);

            // Cập nhật giá
            cartItem.setPriceAtPurchase(product.getPrice());

        } else {
            cartItem = new CartItem();
            cartItem.setCart(cart);
            cartItem.setProduct(product);
            cartItem.setQuantity(quantity);

            // Gán giá sản phẩm
            cartItem.setPriceAtPurchase(product.getPrice());

            if (cart.getItems() == null) {
                cart.setItems(new ArrayList<>());
            }
            cart.getItems().add(cartItem);
        }

        // --- 3. Lưu CartItem & Cart ---
        cartItemRepository.save(cartItem);
        cartRepository.save(cart);

        return true;
    }

    @Transactional
    public boolean updateCartItemQuantity(Long cartItemId, int newQuantity) {
        if (newQuantity <= 0) {
            // Nếu số lượng là 0 hoặc âm, coi như xóa CartItem đó
            return removeCartItem(cartItemId);
        }

        Optional<CartItem> itemOptional = cartItemRepository.findById(cartItemId);

        if (itemOptional.isPresent()) {
            CartItem item = itemOptional.get();
            item.setQuantity(newQuantity);

            // Lưu lại CartItem đã cập nhật
            cartItemRepository.save(item);
            System.out.println("DEBUG: [updateCartItemQuantity] Đã cập nhật Item ID: " + cartItemId + " lên số lượng: " + newQuantity);
            return true;
        }

        System.err.println("❌ LỖI: Không tìm thấy CartItem ID: " + cartItemId + " để cập nhật.");
        return false;
    }

    // ⭐ THÊM PHƯƠNG THỨC HỖ TRỢ XÓA (Cần thiết khi Quantity = 0)
    @Transactional
    public boolean removeCartItem(Long cartItemId) {
        Optional<CartItem> itemOptional = cartItemRepository.findById(cartItemId);

        if (itemOptional.isPresent()) {
            CartItem item = itemOptional.get();

            // Lấy Cart để xóa CartItem khỏi Collection trước khi xóa khỏi DB
            Cart cart = item.getCart();
            if (cart != null && cart.getItems() != null) {
                cart.getItems().remove(item);
            }

            cartItemRepository.delete(item);
            System.out.println("DEBUG: [removeCartItem] Đã xóa CartItem ID: " + cartItemId);
            return true;
        }
        return false;
    }
    @Transactional
    public void deleteCart(Long cartId) {
        if (cartId != null) {
            // Sử dụng CartRepository đã được inject để xóa Cart
            // Giả định rằng bạn đã cấu hình CascadeType.ALL/orphanRemoval=true
            // trong Cart Entity để tự động xóa CartItem khi xóa Cart.
            cartRepository.deleteById(cartId);
            System.out.println("DEBUG: [deleteCart] Đã xóa Cart ID: " + cartId);
        }
    }
}