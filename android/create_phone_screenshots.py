import os
from PIL import Image, ImageDraw, ImageFont

def create_screenshots():
    desktop = os.path.expanduser('~/Desktop')
    img_path = r"C:\Users\dell\.gemini\antigravity\brain\72a4dfaf-b833-4852-b3f4-01395c7fde5a\.user_uploaded\media__1784749243572.png"
    
    logo = Image.open(img_path).convert('RGBA')

    # Screenshot 1: Splash / Welcome Screen (1080 x 1920)
    sc1 = Image.new('RGB', (1080, 1920), (255, 255, 255))
    
    # Scale logo to width ~800
    w, h = logo.size
    target_w = 800
    target_h = int(h * (target_w / w))
    logo_resized = logo.resize((target_w, target_h), Image.Resampling.LANCZOS)
    
    # Paste logo in center
    pos_x = (1080 - target_w) // 2
    pos_y = (1920 - target_h) // 2 - 100
    sc1.paste(logo_resized, (pos_x, pos_y), logo_resized)
    
    # Draw simple UI card below
    draw1 = ImageDraw.Draw(sc1)
    # Blue status bar simulation
    draw1.rectangle([0, 0, 1080, 80], fill=(10, 37, 64))
    # Bottom button simulation
    draw1.rounded_rectangle([140, 1500, 940, 1620], radius=20, fill=(10, 37, 64))
    
    s1_path = os.path.join(desktop, 'LiftnGo_Screenshot_1.png')
    sc1.save(s1_path, 'PNG')
    print(f"Saved Screenshot 1 to {s1_path}")

    # Screenshot 2: Login / Partner Dashboard Screen (1080 x 1920)
    sc2 = Image.new('RGB', (1080, 1920), (245, 247, 250))
    draw2 = ImageDraw.Draw(sc2)
    
    # Top Header
    draw2.rectangle([0, 0, 1080, 240], fill=(10, 37, 64))
    
    # Small header logo
    logo_small_w = 450
    logo_small_h = int(h * (logo_small_w / w))
    logo_small = logo.resize((logo_small_w, logo_small_h), Image.Resampling.LANCZOS)
    sc2.paste(logo_small, ((1080 - logo_small_w) // 2, 70), logo_small)
    
    # Card 1: Partner Login / Status
    draw2.rounded_rectangle([80, 320, 1000, 850], radius=24, fill=(255, 255, 255), outline=(220, 225, 230), width=2)
    # Input fields simulation
    draw2.rounded_rectangle([140, 440, 940, 540], radius=12, fill=(240, 243, 246))
    draw2.rounded_rectangle([140, 580, 940, 680], radius=12, fill=(240, 243, 246))
    draw2.rounded_rectangle([140, 720, 940, 810], radius=12, fill=(10, 37, 64))
    
    # Card 2: Driver Orders / Deliveries
    draw2.rounded_rectangle([80, 920, 1000, 1750], radius=24, fill=(255, 255, 255), outline=(220, 225, 230), width=2)
    draw2.rounded_rectangle([120, 980, 960, 1180], radius=16, fill=(240, 245, 255))
    draw2.rounded_rectangle([120, 1220, 960, 1420], radius=16, fill=(240, 245, 255))
    draw2.rounded_rectangle([120, 1460, 960, 1660], radius=16, fill=(240, 245, 255))
    
    s2_path = os.path.join(desktop, 'LiftnGo_Screenshot_2.png')
    sc2.save(s2_path, 'PNG')
    print(f"Saved Screenshot 2 to {s2_path}")

if __name__ == '__main__':
    create_screenshots()
