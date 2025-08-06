#!/usr/bin/env python3
"""
Script to convert high_match_job_description.txt to a professional PDF format
"""

from reportlab.lib.pagesizes import letter, A4
from reportlab.platypus import SimpleDocTemplate, Paragraph, Spacer, Table, TableStyle
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.lib.units import inch
from reportlab.lib import colors
from reportlab.lib.enums import TA_LEFT, TA_CENTER, TA_JUSTIFY
import os

def create_job_description_pdf():
    # Input and output file paths
    input_file = "src/main/resources/docs/high_match_job_description.txt"
    output_file = "src/main/resources/docs/high_match_job_description.pdf"
    
    # Create the PDF document
    doc = SimpleDocTemplate(
        output_file,
        pagesize=A4,
        rightMargin=72,
        leftMargin=72,
        topMargin=72,
        bottomMargin=72
    )
    
    # Get the default stylesheet and create custom styles
    styles = getSampleStyleSheet()
    
    # Custom styles
    title_style = ParagraphStyle(
        'CustomTitle',
        parent=styles['Heading1'],
        fontSize=24,
        textColor=colors.HexColor('#024950'),
        spaceAfter=12,
        alignment=TA_CENTER,
        fontName='Helvetica-Bold'
    )
    
    company_style = ParagraphStyle(
        'CompanyStyle',
        parent=styles['Heading2'],
        fontSize=16,
        textColor=colors.HexColor('#0FA4AF'),
        spaceAfter=6,
        alignment=TA_CENTER,
        fontName='Helvetica-Bold'
    )
    
    section_header_style = ParagraphStyle(
        'SectionHeader',
        parent=styles['Heading3'],
        fontSize=14,
        textColor=colors.HexColor('#024950'),
        spaceBefore=16,
        spaceAfter=8,
        fontName='Helvetica-Bold'
    )
    
    info_style = ParagraphStyle(
        'InfoStyle',
        parent=styles['Normal'],
        fontSize=11,
        textColor=colors.black,
        spaceAfter=4,
        fontName='Helvetica'
    )
    
    bullet_style = ParagraphStyle(
        'BulletStyle',
        parent=styles['Normal'],
        fontSize=11,
        textColor=colors.black,
        spaceAfter=4,
        leftIndent=20,
        fontName='Helvetica'
    )
    
    # Story array to hold all elements
    story = []
    
    # Read the text file
    try:
        with open(input_file, 'r', encoding='utf-8') as file:
            content = file.read()
    except FileNotFoundError:
        print(f"Error: File {input_file} not found!")
        return False
    
    lines = content.strip().split('\n')
    
    # Process the content
    i = 0
    while i < len(lines):
        line = lines[i].strip()
        
        if not line:  # Skip empty lines
            i += 1
            continue
            
        # Job Title (first line)
        if i == 0:
            story.append(Paragraph(line, title_style))
            story.append(Spacer(1, 12))
        
        # Company information
        elif line.startswith('Company:'):
            story.append(Paragraph(line, company_style))
        elif line.startswith('Location:'):
            story.append(Paragraph(line, info_style))
        elif line.startswith('Experience Level:'):
            story.append(Paragraph(line, info_style))
            story.append(Spacer(1, 12))
        
        # Section headers
        elif line.endswith(':') and not line.startswith('•'):
            story.append(Paragraph(line, section_header_style))
        
        # Bullet points
        elif line.startswith('•'):
            story.append(Paragraph(line, bullet_style))
        
        # Regular paragraphs
        else:
            story.append(Paragraph(line, info_style))
        
        i += 1
    
    # Add a footer with generation info
    story.append(Spacer(1, 24))
    footer_style = ParagraphStyle(
        'Footer',
        parent=styles['Normal'],
        fontSize=9,
        textColor=colors.grey,
        alignment=TA_CENTER,
        fontName='Helvetica-Oblique'
    )
    story.append(Paragraph("Generated from high_match_job_description.txt", footer_style))
    
    # Build the PDF
    try:
        doc.build(story)
        print(f"✅ PDF successfully created: {output_file}")
        return True
    except Exception as e:
        print(f"❌ Error creating PDF: {str(e)}")
        return False

if __name__ == "__main__":
    # Check if reportlab is available
    try:
        import reportlab
        print("📄 Converting job description to PDF...")
        success = create_job_description_pdf()
        if success:
            print("🎉 Conversion completed successfully!")
        else:
            print("💥 Conversion failed!")
    except ImportError:
        print("❌ ReportLab library not found. Installing...")
        print("Please run: pip install reportlab")
        print("Then run this script again.") 