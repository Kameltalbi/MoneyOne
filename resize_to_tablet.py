#!/usr/bin/env python3
"""
Script to resize phone screenshots to tablet format for Google Play Store.
Tablet requirements: 16:9 or 9:16, PNG/JPEG, 320-3840px per side
"""

from PIL import Image
import os
import sys

def resize_to_tablet(input_path, output_path, landscape=False):
    """
    Resize a phone screenshot to tablet format.
    
    Args:
        input_path: Path to input screenshot
        output_path: Path to save resized screenshot
        landscape: If True, create 16:9 landscape (1920x1080), else 9:16 portrait (1080x1920)
    """
    try:
        # Open image
        img = Image.open(input_path)
        
        # Target dimensions for tablet
        if landscape:
            target_size = (1920, 1080)  # 16:9 landscape
        else:
            target_size = (1080, 1920)  # 9:16 portrait
        
        # Calculate scaling to fit within target while maintaining aspect ratio
        img_ratio = img.width / img.height
        target_ratio = target_size[0] / target_size[1]
        
        if img_ratio > target_ratio:
            # Image is wider, scale by width
            new_width = target_size[0]
            new_height = int(new_width / img_ratio)
        else:
            # Image is taller, scale by height
            new_height = target_size[1]
            new_width = int(new_height * img_ratio)
        
        # Resize image
        resized = img.resize((new_width, new_height), Image.Resampling.LANCZOS)
        
        # Create new image with target size and paste resized image centered
        final = Image.new('RGB', target_size, (255, 255, 255))  # White background
        paste_x = (target_size[0] - new_width) // 2
        paste_y = (target_size[1] - new_height) // 2
        final.paste(resized, (paste_x, paste_y))
        
        # Save
        final.save(output_path, 'PNG', quality=95)
        print(f"✅ Created: {output_path}")
        
    except Exception as e:
        print(f"❌ Error processing {input_path}: {e}")

def main():
    # Create output directory
    output_dir = os.path.expanduser("~/Desktop/MoneyOne-Screenshots-Tablet")
    os.makedirs(output_dir, exist_ok=True)
    
    print("📱 MoneyOne Screenshot Resizer for Tablet")
    print("=" * 50)
    print(f"Output directory: {output_dir}")
    print()
    
    # Check if input directory is provided
    if len(sys.argv) < 2:
        print("Usage: python3 resize_to_tablet.py <input_directory>")
        print("Example: python3 resize_to_tablet.py ~/Desktop/phone-screenshots/")
        sys.exit(1)
    
    input_dir = os.path.expanduser(sys.argv[1])
    
    if not os.path.exists(input_dir):
        print(f"❌ Input directory not found: {input_dir}")
        sys.exit(1)
    
    # Process all PNG and JPG files
    files = [f for f in os.listdir(input_dir) if f.lower().endswith(('.png', '.jpg', '.jpeg'))]
    
    if not files:
        print(f"❌ No PNG/JPG files found in {input_dir}")
        sys.exit(1)
    
    print(f"Found {len(files)} screenshot(s)")
    print()
    
    for i, filename in enumerate(sorted(files), 1):
        input_path = os.path.join(input_dir, filename)
        output_filename = f"tablet_screenshot_{i}.png"
        output_path = os.path.join(output_dir, output_filename)
        
        # Use portrait format (9:16) by default for phone screenshots
        resize_to_tablet(input_path, output_path, landscape=False)
    
    print()
    print(f"✅ Done! {len(files)} tablet screenshots created in:")
    print(f"   {output_dir}")
    print()
    print("📤 You can now upload these to Google Play Console (Tablet section)")

if __name__ == "__main__":
    main()
