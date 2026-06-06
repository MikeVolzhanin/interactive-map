import { FileQuestion } from 'lucide-react';

interface EmptyStateProps {
  message: string;
}

export function EmptyState({ message }: EmptyStateProps) {
  return (
    <div className="flex flex-col items-center justify-center py-16 gap-4">
      <FileQuestion className="h-12 w-12" style={{ color: 'var(--text-secondary)' }} />
      <p style={{ color: 'var(--text-secondary)' }}>{message}</p>
    </div>
  );
}
