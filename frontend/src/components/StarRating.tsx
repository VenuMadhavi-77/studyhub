import React, { useState } from 'react';
import { Star } from 'lucide-react';

interface StarRatingProps {
  rating: number;
  maxRating?: number;
  size?: 'sm' | 'md' | 'lg';
  interactive?: boolean;
  onRatingChange?: (rating: number) => void;
  showText?: boolean;
  reviewsCount?: number;
}

export const StarRating: React.FC<StarRatingProps> = ({
  rating,
  maxRating = 5,
  size = 'md',
  interactive = false,
  onRatingChange,
  showText = true,
  reviewsCount,
}) => {
  const [hoverRating, setHoverRating] = useState<number | null>(null);

  const starSizes = {
    sm: 14,
    md: 18,
    lg: 24,
  }[size];

  const currentDisplay = hoverRating !== null ? hoverRating : rating;

  return (
    <div className="inline-flex items-center gap-1.5">
      <div className="flex items-center gap-0.5" onMouseLeave={() => interactive && setHoverRating(null)}>
        {Array.from({ length: maxRating }, (_, index) => {
          const starValue = index + 1;
          const isFilled = starValue <= currentDisplay;
          const isHalf = !isFilled && starValue - 0.5 <= currentDisplay;

          return (
            <button
              key={index}
              type="button"
              disabled={!interactive}
              onClick={() => interactive && onRatingChange && onRatingChange(starValue)}
              onMouseEnter={() => interactive && setHoverRating(starValue)}
              className={`transition-transform duration-150 ${interactive ? 'cursor-pointer hover:scale-125' : 'cursor-default'}`}
              aria-label={`Rate ${starValue} stars`}
            >
              <Star
                size={starSizes}
                className={`${
                  isFilled
                    ? 'fill-amber-400 text-amber-400'
                    : isHalf
                    ? 'fill-amber-200 text-amber-400'
                    : 'text-slate-300 dark:text-slate-600'
                }`}
              />
            </button>
          );
        })}
      </div>

      {showText && (
        <span className="text-xs font-semibold text-slate-700 dark:text-slate-300 ml-0.5">
          {rating > 0 ? rating.toFixed(1) : 'New'}
          {reviewsCount !== undefined && (
            <span className="text-slate-400 dark:text-slate-500 font-normal ml-1">
              ({reviewsCount})
            </span>
          )}
        </span>
      )}
    </div>
  );
};
