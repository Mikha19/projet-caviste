import React from 'react';
import type { Metadata } from 'next';
import './globals.css';

export const metadata: Metadata = {
  title: 'CaveManager - Gestion de Cave',
  description: 'Plateforme de gestion et de vente en ligne de vins',
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="fr">
      <body className="bg-light text-gray-900">
        {children}
      </body>
    </html>
  );
}
