import React, { useState } from 'react';
import {
    Container,
    Typography,
    Button,
    Dialog,
    DialogTitle,
    DialogContent,
    Snackbar,
    Alert,
    Box
} from '@mui/material';
import { Add as AddIcon } from '@mui/icons-material';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { productApi } from '../api/productApi';
import { ProductForm } from '../components/ProductForm';
import { ProductList } from '../components/ProductList';
import type { Product, ProductFormData } from '../types/product';

export const ProductsPage: React.FC = () => {
    const [isDialogOpen, setIsDialogOpen] = useState(false);
    const [selectedProduct, setSelectedProduct] = useState<Product | undefined>();
    const [error, setError] = useState<string>();
    const queryClient = useQueryClient();

    const { data: products = [], isLoading } = useQuery({
        queryKey: ['products'],
        queryFn: productApi.getAllProducts
    });

    const createMutation = useMutation({
        mutationFn: productApi.createProduct,
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['products'] });
            setIsDialogOpen(false);
            setError(undefined);
        },
        onError: (error: Error) => {
            setError(error.message);
        }
    });

    const updateMutation = useMutation({
        mutationFn: productApi.updateProduct,
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['products'] });
            setIsDialogOpen(false);
            setError(undefined);
        },
        onError: (error: Error) => {
            setError(error.message);
        }
    });

    const deleteMutation = useMutation({
        mutationFn: productApi.deleteProduct,
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['products'] });
        }
    });

    const handleSubmit = (data: ProductFormData) => {
        if (selectedProduct) {
            updateMutation.mutate({ ...data, id: selectedProduct.id });
            setSelectedProduct(undefined);
        } else {
            createMutation.mutate(data);
        }
    };

    const handleEdit = (product: Product) => {
        setSelectedProduct(product);
        setIsDialogOpen(true);
    };

    const handleDelete = (id: number) => {
        if (window.confirm('Opravdu chcete smazat tento produkt?')) {
            deleteMutation.mutate(id);
        }
    };

    const handleCloseDialog = () => {
        setIsDialogOpen(false);
        setSelectedProduct(undefined);
        setError(undefined);
    };

    return (
        <Box sx={{ 
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            width: '100%',
            minHeight: '100vh',
            bgcolor: 'background.default'
        }}>
            <Container maxWidth="lg" sx={{ 
                py: 4,
                display: 'flex',
                flexDirection: 'column',
            }}>
                <Typography variant="h4" component="h1" gutterBottom sx={{ textAlign: 'center' }}>
                    Správa produktů
                </Typography>

                <Button
                    variant="contained"
                    startIcon={<AddIcon />}
                    onClick={() => setIsDialogOpen(true)}
                    sx={{ mb: 2 }}
                >
                    Přidat produkt
                </Button>

                {isLoading ? (
                    <Typography sx={{ textAlign: 'center' }}>Načítání...</Typography>
                ) : (
                    <Box sx={{ width: '100%' }}>
                        <ProductList
                            products={products}
                            onEdit={handleEdit}
                            onDelete={handleDelete}
                        />
                    </Box>
                )}

                <Dialog open={isDialogOpen} onClose={handleCloseDialog} maxWidth="sm" fullWidth>
                    <DialogTitle>
                        {selectedProduct ? 'Upravit produkt' : 'Nový produkt'}
                    </DialogTitle>
                    <DialogContent>
                        <ProductForm
                            initialData={selectedProduct}
                            onSubmit={handleSubmit}
                            error={error}
                        />
                    </DialogContent>
                </Dialog>

                <Snackbar
                    open={!!error}
                    autoHideDuration={6000}
                    onClose={() => setError(undefined)}
                >
                    <Alert severity="error" onClose={() => setError(undefined)}>
                        {error}
                    </Alert>
                </Snackbar>
            </Container>
        </Box>
    );
}; 
