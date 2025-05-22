import axios from 'axios';
import type {Product} from '../types/product';

const API_URL = 'http://localhost:8080/api/v1';

export const productApi = {
    getAllProducts: async (): Promise<Product[]> => {
        const response = await axios.get(`${API_URL}/products/`);
        return response.data;
    },

    createProduct: async (product: Product): Promise<Product> => {
        const response = await axios.post(`${API_URL}/products/`, product);
        return response.data;
    },

    updateProduct: async (product: Product): Promise<Product> => {
        const response = await axios.put(`${API_URL}/products/${product.id}`, product);
        return response.data;
    },

    deleteProduct: async (id: number): Promise<boolean> => {
        const response = await axios.delete(`${API_URL}/products/${id}`);
        return response.data;
    }
}; 
