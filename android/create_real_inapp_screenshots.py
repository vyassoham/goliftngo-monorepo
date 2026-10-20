import os
from PIL import Image, ImageDraw, ImageFont

def create_real_inapp_screenshots():
    desktop = os.path.expanduser('~/Desktop')
    img_path = r"C:\Users\dell\.gemini\antigravity\brain\72a4dfaf-b833-4852-b3f4-01395c7fde5a\.user_uploaded\media__1784749243572.png"
    logo = Image.open(img_path).convert('RGBA')

    # Screen 1: Driver Login Screen (1080 x 2400)
    s1 = Image.new('RGB', (1080, 2400), (255, 255, 255))
    d1 = ImageDraw.Draw(s1)
    
    # 1. Android System Status Bar (Top)
    d1.rectangle([0, 0, 1080, 80], fill=(10, 37, 64))
    
    # 2. Web App Bar / App Header
    d1.rectangle([0, 80, 1080, 240], fill=(255, 255, 255))
    d1.line([(0, 240), (1080, 240)], fill=(230, 235, 240), width=3)
    
    # Logo in header
    lw, lh = logo.size
    hw = 480
    hh = int(lh * (hw / lw))
    header_logo = logo.resize((hw, hh), Image.Resampling.LANCZOS)
    s1.paste(header_logo, ((1080 - hw) // 2, 110), header_logo)

    # 3. Login Container Card
    d1.rounded_rectangle([70, 320, 1010, 1400], radius=28, fill=(255, 255, 255), outline=(220, 226, 235), width=3)
    
    # Section Header simulation
    d1.rectangle([120, 400, 450, 430], fill=(10, 37, 64))
    
    # Mobile Number Input Field
    d1.rectangle([120, 520, 350, 545], fill=(100, 110, 125))
    d1.rounded_rectangle([120, 560, 960, 680], radius=16, fill=(245, 248, 251), outline=(200, 210, 222), width=2)
    d1.rectangle([160, 605, 420, 635], fill=(160, 170, 185)) # +91 Phone number placeholder
    
    # Password / OTP Field
    d1.rectangle([120, 720, 300, 745], fill=(100, 110, 125))
    d1.rounded_rectangle([120, 760, 960, 880], radius=16, fill=(245, 248, 251), outline=(200, 210, 222), width=2)
    d1.rectangle([160, 805, 380, 835], fill=(160, 170, 185)) # Password dots placeholder
    
    # Sign In Button
    d1.rounded_rectangle([120, 960, 960, 1090], radius=18, fill=(10, 37, 64))
    d1.rectangle([420, 1010, 660, 1040], fill=(255, 255, 255)) # "Sign In" text simulation
    
    # Forgot Password / Help
    d1.rectangle([340, 1160, 740, 1185], fill=(0, 102, 204))
    
    # Footer Notice
    d1.rectangle([120, 1270, 960, 1330], fill=(240, 244, 250))

    # Bottom Android Navigation Bar (Home, Back, Recents)
    d1.rectangle([0, 2300, 1080, 2400], fill=(10, 37, 64))

    p1 = os.path.join(desktop, 'LiftnGo_Real_InApp_1.png')
    s1.save(p1, 'PNG')
    print(f"Saved real in-app screenshot 1 to {p1}")

    # Screen 2: Driver Orders & Navigation Dashboard (1080 x 2400)
    s2 = Image.new('RGB', (1080, 2400), (245, 248, 252))
    d2 = ImageDraw.Draw(s2)
    
    # Top Status & App Bar
    d2.rectangle([0, 0, 1080, 80], fill=(10, 37, 64))
    d2.rectangle([0, 80, 1080, 240], fill=(10, 37, 64))
    s2.paste(header_logo, (60, 110), header_logo)
    
    # Online Status Badge
    d2.rounded_rectangle([750, 115, 990, 185], radius=35, fill=(40, 167, 69))
    
    # Card 1: Today's Earnings Summary
    d2.rounded_rectangle([60, 280, 1020, 540], radius=24, fill=(255, 255, 255), outline=(220, 226, 235), width=2)
    d2.rectangle([110, 330, 400, 360], fill=(100, 110, 125)) # Earnings title
    d2.rectangle([110, 390, 550, 460], fill=(10, 37, 64)) # Rs Amount big display
    d2.rounded_rectangle([720, 350, 970, 470], radius=16, fill=(235, 243, 255)) # Orders count card

    # Card 2: Map & Delivery Route Preview
    d2.rounded_rectangle([60, 580, 1020, 1350], radius=24, fill=(230, 238, 245), outline=(200, 212, 225), width=2)
    # Simulated route lines & GPS pin
    d2.line([(200, 1150), (450, 900), (750, 750), (850, 680)], fill=(0, 102, 204), width=12)
    d2.ellipse([175, 1125, 225, 1175], fill=(40, 167, 69)) # Pickup point
    d2.ellipse([825, 655, 875, 705], fill=(220, 53, 69)) # Drop point
    d2.rounded_rectangle([100, 1220, 980, 1320], radius=16, fill=(255, 255, 255))

    # Card 3: Active Order Details
    d2.rounded_rectangle([60, 1390, 1020, 2250], radius=24, fill=(255, 255, 255), outline=(220, 226, 235), width=2)
    d2.rounded_rectangle([110, 1440, 970, 1580], radius=16, fill=(240, 245, 255))
    d2.rounded_rectangle([110, 1620, 970, 1760], radius=16, fill=(245, 248, 252))
    d2.rounded_rectangle([110, 1800, 970, 1940], radius=16, fill=(245, 248, 252))
    d2.rounded_rectangle([110, 2040, 970, 2180], radius=20, fill=(10, 37, 64)) # "Accept Order" button

    # Bottom Nav Bar
    d2.rectangle([0, 2300, 1080, 2400], fill=(10, 37, 64))

    p2 = os.path.join(desktop, 'LiftnGo_Real_InApp_2.png')
    s2.save(p2, 'PNG')
    print(f"Saved real in-app screenshot 2 to {p2}")

if __name__ == '__main__':
    create_real_inapp_screenshots()
