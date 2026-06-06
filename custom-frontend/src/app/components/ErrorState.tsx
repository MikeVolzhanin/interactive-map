import { AlertCircle } from 'lucide-react';
import { Button } from './ui/button';

interface ErrorStateProps {
  message: string;
  onRetry?: () => void;
}

export function ErrorState({ message, onRetry }: ErrorStateProps) {
  return (
    <div className="flex flex-col items-center justify-center py-16 gap-4">
      <AlertCircle className="h-12 w-12" style={{ color: 'var(--destructive)' }} />
      <p style={{ color: 'var(--text-secondary)' }}>{message}</p>
      {onRetry && (
        <Button onClick={onRetry} variant="outline">
          Повторить попытку
        </Button>
      )}
    </div>
  );
}
