import React from 'react';
import { useForm, Controller } from 'react-hook-form';
import type { ControllerRenderProps } from 'react-hook-form';
import { TextField, Button, Box, Alert } from '@mui/material';
import type { ProductFormData } from '../types/product';

interface ProductFormProps {
    initialData?: ProductFormData;
    onSubmit: (data: ProductFormData) => void;
    error?: string;
}

export const ProductForm: React.FC<ProductFormProps> = ({ initialData, onSubmit, error }) => {
    const { control, handleSubmit, formState: { errors } } = useForm<ProductFormData>({
        defaultValues: initialData || {
            name: '',
            quantity: 0,
            pricePerUnit: 0
        }
    });

    return (
        <Box component="form" onSubmit={handleSubmit(onSubmit)} sx={{ mt: 2 }}>
            {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
            
            <Controller
                name="name"
                control={control}
                rules={{ required: 'Název produktu je povinný' }}
                render={({ field }: { field: ControllerRenderProps<ProductFormData, 'name'> }) => (
                    <TextField
                        {...field}
                        label="Název produktu"
                        fullWidth
                        margin="normal"
                        error={!!errors.name}
                        helperText={errors.name?.message}
                    />
                )}
            />

            <Controller
                name="quantity"
                control={control}
                rules={{
                    required: 'Množství je povinné',
                    min: { value: 0, message: 'Množství musí být nezáporné' }
                }}
                render={({ field }: { field: ControllerRenderProps<ProductFormData, 'quantity'> }) => (
                    <TextField
                        {...field}
                        type="number"
                        label="Množství na skladě"
                        fullWidth
                        margin="normal"
                        error={!!errors.quantity}
                        helperText={errors.quantity?.message}
                    />
                )}
            />

            <Controller
                name="pricePerUnit"
                control={control}
                rules={{
                    required: 'Cena je povinná',
                    min: { value: 0, message: 'Cena musí být nezáporná' }
                }}
                render={({ field }: { field: ControllerRenderProps<ProductFormData, 'pricePerUnit'> }) => (
                    <TextField
                        {...field}
                        type="number"
                        label="Cena za kus"
                        fullWidth
                        margin="normal"
                        error={!!errors.pricePerUnit}
                        helperText={errors.pricePerUnit?.message}
                    />
                )}
            />

            <Button
                type="submit"
                variant="contained"
                color="primary"
                fullWidth
                sx={{ mt: 2 }}
            >
                {initialData ? 'Upravit produkt' : 'Vytvořit produkt'}
            </Button>
        </Box>
    );
}; 