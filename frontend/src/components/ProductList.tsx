import React from 'react';
import {
    Table,
    TableBody,
    TableCell,
    TableContainer,
    TableHead,
    TableRow,
    Paper,
    IconButton,
    Typography
} from '@mui/material';
import { Edit as EditIcon, Delete as DeleteIcon } from '@mui/icons-material';
import type { Product } from '../types/product';

interface ProductListProps {
    products: Product[];
    onEdit: (product: Product) => void;
    onDelete: (id: number) => void;
}

export const ProductList: React.FC<ProductListProps> = ({ products, onEdit, onDelete }) => {
    if (products.length === 0) {
        return (
            <Typography variant="body1" sx={{ mt: 2, textAlign: 'center' }}>
                Žádné produkty k zobrazení
            </Typography>
        );
    }

    return (
        <TableContainer component={Paper} sx={{ mt: 2 }}>
            <Table>
                <TableHead>
                    <TableRow>
                        <TableCell>Název</TableCell>
                        <TableCell align="right">Množství na skladě</TableCell>
                        <TableCell align="right">Cena za kus</TableCell>
                        <TableCell align="right">Akce</TableCell>
                    </TableRow>
                </TableHead>
                <TableBody>
                    {products.map((product) => (
                        <TableRow key={product.id}>
                            <TableCell>{product.name}</TableCell>
                            <TableCell align="right">{product.quantity}</TableCell>
                            <TableCell align="right">{product.pricePerUnit} Kč</TableCell>
                            <TableCell align="right">
                                <IconButton
                                    onClick={() => onEdit(product)}
                                    color="primary"
                                    size="small"
                                >
                                    <EditIcon />
                                </IconButton>
                                <IconButton
                                    onClick={() => product.id && onDelete(product.id)}
                                    color="error"
                                    size="small"
                                >
                                    <DeleteIcon />
                                </IconButton>
                            </TableCell>
                        </TableRow>
                    ))}
                </TableBody>
            </Table>
        </TableContainer>
    );
}; 